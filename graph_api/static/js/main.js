import * as THREE from "three";
import ForceGraph3D from "3d-force-graph";
import SpriteText from "three-spritetext";
import {CSS2DRenderer} from "three/addons/renderers/CSS2DRenderer.js";
import {TextGeometry} from "three/addons/geometries/TextGeometry";
import {FontLoader} from "three/addons/loaders/FontLoader";
import ForceGraph from "force-graph";

function vertexBuilder(id, name, color) {
  return {
    id,
    name,
    color,
  };
}

function edgeBuilder(id, source, target, label) {
  return {
    id,
    source,
    target,
    label
  }
}

function edgeWithCurvatureBuilder(id, source, target, label, curvature) {
  return {
    id,
    source,
    target,
    label,
    curvature
  }
}

const state = {
  data: {
    vertices: [], // states
    edges: [], // program function
  },
  options: {
    mode: false, // true: 3D; false: 2D
    particles: true,
    focusNode: false,
    textNode: false,
  }
};

verticesData.forEach(vertex => {
  const {id, name} = vertex;
  state.data.vertices.push(vertexBuilder(id, name, "#ff0000"));
});

function findVertexNameById(id) {
  return state.data.vertices.find(v => v.id === id).name;
}

edgesData.forEach(edge => {
  const {id, source_id, target_id, description} = edge;

  const source = findVertexNameById(source_id);
  const target = findVertexNameById(target_id);

  state.data.edges.push(edgeBuilder(id, source, target, description));
});

const socket = new WebSocket('ws://' + window.location.host + '/ws/graph/');

socket.onopen = (event) => {
  console.log("Websocket connected");
};

socket.onmessage = (event) => {
  const data = JSON.parse(event.data).data;
  
  console.log("data:", data);

  if (data.type === 'vertex') {
    state.data.vertices.push(vertexBuilder(data.obj.id, data.obj.name, "#ff0000"));
  } else if (data.type === 'edge') {
    const source = findVertexNameById(data.obj.source);
    const target = findVertexNameById(data.obj.target);
    state.data.edges.push(edgeBuilder(data.obj.id, source, target, data.obj.description));
  }

  console.log("state:", state);

  refreshGraph();
};

socket.onclose = (event) => {
  console.error('Chat socket closed unexpectedly');
};

function prepareGraph() {
  const nodes = [];
  const links = [];

    // Criar os nós
  state.data.vertices.forEach(vertex => {
    nodes.push({
      id: vertex.name,
      color: vertex.color,
    });
  });

  // Criar os links
  state.data.edges.forEach(edge => {
    if (edge) {
      links.push({
        curvature: 0.3,
        ...edge
      });
    }
  });

  // Criar o objeto do grafo
  const graph = {
    nodes,
    links
  };

  return(graph)
}

function refreshGraph() {
  setTimeout(() => {
    function getQuadraticXY3D(t, sx, sy, sz, cp1x, cp1y, cp1z, ex, ey, ez) {
      return {
        x: (1 - t) * (1 - t) * sx + 2 * (1 - t) * t * cp1x + t * t * ex,
        y: (1 - t) * (1 - t) * sy + 2 * (1 - t) * t * cp1y + t * t * ey,
        z: (1 - t) * (1 - t) * sz + 2 * (1 - t) * t * cp1z + t * t * ez
      };
    }

    function getQuadraticXY2D(t, sx, sy, cp1x, cp1y, ex, ey) {
      return {
        x: (1 - t) * (1 - t) * sx + 2 * (1 - t) * t * cp1x + t * t * ex,
        y: (1 - t) * (1 - t) * sy + 2 * (1 - t) * t * cp1y + t * t * ey,
      };
    }

    if (state.options.mode) {
      // const variables = this.getVariables();

      const graph = prepareGraph();

      let Graph = ForceGraph3D({
        extraRenderers: [new CSS2DRenderer()]
      })
      (document.getElementById('3d-graph'))
      // .width(document.getElementById('container').offsetWidth)
      // .height(document.getElementById('container').offsetHeight)
      Graph.nodeThreeObject(node => {
        const geometry = new THREE.SphereGeometry(5, 22, 32);
        const material = new THREE.MeshLambertMaterial({
          color: node.color,
          transparent: true,
          opacity: 0.7
        });

        const sphere = new THREE.Mesh(geometry, material);

        const loader = new FontLoader();
        loader.load('https://threejs.org/examples/fonts/helvetiker_regular.typeface.json', function (font) {
          const textGeometry = new TextGeometry(node.id, {
            font: font,
            size: 3,
            height: 0.5,
            curveSegments: 12,
            bevelEnabled: false
          });

          const textMaterial = new THREE.MeshLambertMaterial({color: 0xffffff});
          const textMesh = new THREE.Mesh(textGeometry, textMaterial);

          // Centralizar o texto na esfera
          const sphereCenter = new THREE.Vector3();
          geometry.computeBoundingBox();
          geometry.boundingBox.getCenter(sphereCenter);

          const textBoundingBox = new THREE.Box3().setFromObject(textMesh);
          const textCenter = new THREE.Vector3();
          textBoundingBox.getCenter(textCenter);

          textMesh.position.copy(sphereCenter);
          textMesh.position.sub(textCenter);

          sphere.add(textMesh);
        });

        return sphere;
      })
        .graphData(graph)
        .linkLabel('label')
        .linkThreeObjectExtend(true)
        .linkThreeObject(link => {
          const sprite = new SpriteText(`${link.label}`);
          sprite.color = 'lightgrey';
          sprite.textHeight = 1.5;
          sprite.name = link;
          return sprite;
        })
        .linkPositionUpdate((sprite, {start, end}, link) => {

          let middlePos = getQuadraticXY3D(
            0.5,
            start.x,
            start.y,
            start.z,
            link.__curve.v1.x,
            link.__curve.v1.y,
            link.__curve.v1.z,
            end.x,
            end.y,
            end.z
          );

          // Position sprite
          Object.assign(sprite.position, middlePos);


        })
        .linkDirectionalArrowLength(2)
        .linkDirectionalArrowRelPos(1)
        .linkCurvature('curvature')
        .cameraPosition({z: 90})

      if (state.options.particles) {
        Graph
          .linkDirectionalParticles("target")
          .linkDirectionalParticleSpeed(d => 0.01)
          .cameraPosition({z: 90})

      }
      if (state.options.focusNode) {
        Graph.onNodeClick(node => {
          const distance = 40;
          const distRatio = 1 + distance / Math.hypot(node.x, node.y, node.z);

          const newPos = node.x || node.y || node.z
            ? {x: node.x * distRatio, y: node.y * distRatio, z: node.z * distRatio}
            : {x: 0, y: 0, z: distance}; // special case if node is in (0,0,0)

          Graph.cameraPosition(
            newPos, // new position
            node, // lookAt ({ x, y, z })
            3000  // ms transition duration
          );
        });
      }
      if (state.options.textNode) {
        Graph.nodeThreeObject(node => {
          const sprite = new SpriteText(node.id);
          sprite.material.depthWrite = false; // make sprite background transparent
          sprite.color = node.color;
          sprite.textHeight = 8;
          return sprite;
        })
      }

    } else {
      // const variables = this.getVariables();
      const graph = prepareGraph();

      const Graph = ForceGraph()
      (document.getElementById('graph'))
        .linkLabel('label')
        .linkAutoColorBy("#fff")
        .backgroundColor("#000")
        .nodeCanvasObject((node, ctx) => {
          ctx.beginPath();
          ctx.arc(node.x, node.y, 5, 0, 2 * Math.PI, false);
          ctx.fillStyle = node.color;
          ctx.fill();

          // Estilizar o texto
          ctx.fillStyle = 'white';
          ctx.font = 'bold 8px Arial';
          ctx.textAlign = 'center';
          ctx.textBaseline = 'middle';

          // Definir a letra a ser exibida
          const letter = node.id;

          // Posicionar o texto no centro do círculo
          ctx.fillText(letter, node.x, node.y);

        })
        .linkDirectionalArrowLength(2)
        .linkCurvature(0.3)
        .zoom(15, 50)
        .linkCanvasObjectMode(() => 'after')
        .linkCanvasObject((link, ctx) => {
          const MAX_FONT_SIZE = 4;
          const LABEL_NODE_MARGIN = Graph.nodeRelSize() * 1.5;

          const start = link.source;
          const end = link.target;

          // ignore unbound links
          if (typeof start !== 'object' || typeof end !== 'object') return;
          let middlePos = getQuadraticXY2D(
            0.5,
            start.x,
            start.y,
            link.__controlPoints[0],
            link.__controlPoints[1],
            end.x,
            end.y
          );

          const relLink = {x: end.x - start.x, y: end.y - start.y};

          const maxTextLength = Math.sqrt(Math.pow(relLink.x, 2) + Math.pow(relLink.y, 2)) - LABEL_NODE_MARGIN * 2;

          let textAngle = Math.atan2(relLink.y, relLink.x);
          // maintain label vertical orientation for legibility
          if (textAngle > Math.PI / 2) textAngle = -(Math.PI - textAngle);
          if (textAngle < -Math.PI / 2) textAngle = -(-Math.PI - textAngle);

          const label = `${link.label}`;

          // estimate fontSize to fit in link length
          ctx.font = '1px Sans-Serif';
          const fontSize = Math.min(MAX_FONT_SIZE, maxTextLength / ctx.measureText(label).width);
          ctx.font = `${fontSize}px Sans-Serif`;
          const textWidth = ctx.measureText(label).width;
          const bckgDimensions = [textWidth, fontSize].map(n => n + fontSize * 0.2); // some padding

          // draw text label (with background rect)
          ctx.save();
          ctx.translate(middlePos.x, middlePos.y);
          ctx.rotate(textAngle);

          ctx.fillStyle = 'rgba(0, 0, 0, 0)'; // Transparente
          ctx.fillRect(-bckgDimensions[0] / 2, -bckgDimensions[1] / 2, ...bckgDimensions);

          ctx.fillStyle = 'white'; // Branco
          ctx.textAlign = 'center';
          ctx.textBaseline = 'middle';
          ctx.fillText(label, 0, 0);
          ctx.restore();
        })
        .linkDirectionalArrowRelPos(1)
        .graphData(graph);

    }

  }, 0o100);
}

refreshGraph()
