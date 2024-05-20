const socket = new WebSocket('ws://' + window.location.host + '/ws/graph/');

socket.onmessage = (event) => {
    const graphData = JSON.parse(event.data);
    console.error("graphData:", graphData);
    // Update your UI with the received graph data
    // For example, append new vertices or edges to your list
};

socket.onclose = (event) => {
    console.error('Chat socket closed unexpectedly');
};

