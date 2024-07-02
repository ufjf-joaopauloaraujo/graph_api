import * as THREE from "three";
import ForceGraph3D from "3d-force-graph";
import SpriteText from "three-spritetext";
import {CSS2DRenderer} from "three/addons/renderers/CSS2DRenderer.js";
import {TextGeometry} from "three/addons/geometries/TextGeometry";
import {FontLoader} from "three/addons/loaders/FontLoader";
import ForceGraph from "force-graph";
import { GUI } from 'three/addons/libs/lil-gui.module.min.js';

function vertexBuilder(id, name, color) {
  return {
    id,
    name,
    color,
  };
}

function edgeBuilder(id, name, source, target, description) {
  return {
    id,
    name,
    source,
    target,
    description
  }
}

function edgeWithCurvatureBuilder(name, source, target, description, curvature) {
  return {
    id,
    name,
    source,
    target,
    description,
    curvature
  }
}

const state = {
  data: {
    vertices: [], // states
    edges: [], // program function
  },
  options: {
    '3dMode': false, // true: 3D; false: 2D
    particles: false,
    focusNode: false,
    textNode: false,
  }
};

verticesData.forEach(vertex => {
  const {id, name, color} = vertex;
  state.data.vertices.push(vertexBuilder(id, name, color));
});

edgesData.forEach(edge => {
  const {id, name, source_id, target_id, description} = edge;
  state.data.edges.push(edgeBuilder(id, name, source_id, target_id, description));
});

let refreshGraphTimeout = null;

function startWebsocket() {
  let socket = new WebSocket('ws://' + window.location.host + '/ws/graph/');

  socket.onopen = (event) => {
    console.log("Websocket connected");
  };

  socket.onmessage = (event) => {
    const data = JSON.parse(event.data).data;
    
    console.log("data:", data);

    if (data.operation === 'create') {
      if (data.type === 'vertex') {
        const {id, name, color} = data.obj;
        state.data.vertices.push(vertexBuilder(id, name, color));
      } else if (data.type === 'edge') {
        const {id, name, source, target, description} = data.obj;
        state.data.edges.push(edgeBuilder(id, name, source, target, description));
      }
    } else if (data.operation === 'update') {
      if (data.type === 'vertex') {
        const {id, name, color} = data.obj;
        const v = state.data.vertices.find(v => v.id === id);
        v.name = name;
        v.color = color;
      } else if (data.type === 'edge') {
        const {id, name, source, target, description} = data.obj;
        const e = state.data.edges.find(e => e.id === id);
        e.name = name;
        e.source = source;
        e.target = target;
        e.description = description;
      }
    } else if (data.operation === 'delete') {
      if (data.type === 'vertex') {
        const {id} = data.obj;
        state.data.vertices = state.data.vertices.filter(v => v.id !== id);
        state.data.edges = state.data.edges.filter(e => e.source !== id && e.target !== id);
      } else if (data.type === 'edge') {
        const {id} = data.obj;
        state.data.edges = state.data.edges.filter(v => v.id !== id);
      } else if (data.type === 'all') {
        state.data.vertices = [];
        state.data.edges = [];
      }
    }

    console.log("state:", state);

    if (refreshGraphTimeout !== null) {
      clearInterval(refreshGraphTimeout);
    }
    refreshGraphTimeout = setTimeout(() => refreshGraph(), 500);
  };

  socket.onclose = (event) => {
    console.error('Chat socket closed unexpectedly');
    // connection closed, discard old websocket and create a new one in 5s
    socket = null;
    setTimeout(startWebsocket, 5000);
  };
}

console.log("state:", state);

startWebsocket();

function prepareGraph() {
  const nodes = [];
  const links = [];

    // Criar os nós
  state.data.vertices.forEach(vertex => {
    nodes.push({
      id: vertex.id,
      name: vertex.name,
      color: vertex.color,
    });
  });

  // Criar os links
  state.data.edges.forEach(edge => {
    if (edge) {
      links.push({
        curvature: edge.curvature || 0.3,
        ...edge,
        label: edge.description,
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

let gui = new GUI();

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

    gui.destroy();

    if (state.options['3dMode']) {

      const graph = prepareGraph();

      let Graph = ForceGraph3D({
        extraRenderers: [new CSS2DRenderer()]
      })
      (document.getElementById('graph'));
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
          const textGeometry = new TextGeometry(node.name, {
            font: font,
            size: 3,
            depth: 0.5,
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
          .linkDirectionalParticles(3)
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
          const sprite = new SpriteText(node.name);
          sprite.material.depthWrite = false; // make sprite background transparent
          sprite.color = node.color;
          sprite.textHeight = 8;
          return sprite;
        })
      }

      setTimeout(() => Graph.zoomToFit(1000, 1), 1000);

      const guiSetup = {
        'Restart camera': () => Graph.zoomToFit(1000, 100),
      };

      gui = new GUI();

      gui.add( state.options, '3dMode' ).onChange( refreshGraph );
      gui.add( state.options, 'particles' ).onChange( refreshGraph );
      gui.add( state.options, 'focusNode' ).onChange( refreshGraph );
      gui.add( state.options, 'textNode' ).onChange( refreshGraph );
      gui.add(guiSetup, 'Restart camera');
      gui.open();
    } else {
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
          ctx.fillStyle = ['white', 'yellow', 'beige'].includes(node.color) ? 'black' : 'white';
          ctx.font = 'bold 8px Arial';
          ctx.textAlign = 'center';
          ctx.textBaseline = 'middle';

          // Definir a letra a ser exibida
          const letter = node.name;

          // Posicionar o texto no centro do círculo
          ctx.fillText(letter, node.x, node.y);

        })
        .linkDirectionalArrowLength(2)
        .linkCurvature(0.3)
        .zoom(10, 50)
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

        if (state.options.particles) {
          Graph
            .linkDirectionalParticles(3)
            .linkDirectionalParticleSpeed(d => 0.01);
        }

        setTimeout(() => Graph.zoomToFit(1000, 100), 2000);

        const guiSetup = {
          'Restart camera': () => Graph.zoomToFit(1000, 100),
        };

        gui = new GUI();

				gui.add( state.options, '3dMode' ).onChange( refreshGraph );
				gui.add( state.options, 'particles' ).onChange( refreshGraph );
        gui.add(guiSetup, 'Restart camera');
				gui.open();
    }

  }, 0o100);
}

refreshGraph();
