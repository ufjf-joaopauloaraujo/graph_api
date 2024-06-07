# graph_api/views.py

from django.shortcuts import render
from rest_framework import viewsets
from .models import Vertex, Edge
from .serializers import VertexSerializer, EdgeSerializer
from channels.layers import get_channel_layer
from asgiref.sync import async_to_sync

class VertexViewSet(viewsets.ModelViewSet):
    queryset = Vertex.objects.all()
    serializer_class = VertexSerializer
    
    def perform_create(self, serializer):
        # Save the new vertex
        vertex = serializer.save()

        # Send the updated graph data to connected clients
        channel_layer = get_channel_layer()
        async_to_sync(channel_layer.group_send)(
            'graph_updates', {
                'type': 'send.data', 
                'data': {
                    'type': 'vertex',
                    'obj': self.serializer_class(vertex).data
                },
             }  # Customize this data as needed
        )


class EdgeViewSet(viewsets.ModelViewSet):
    queryset = Edge.objects.all()
    serializer_class = EdgeSerializer
    
    def perform_create(self, serializer):
        # Save the new edge
        edge = serializer.save()

        # Send the updated graph data to connected clients
        channel_layer = get_channel_layer()
        async_to_sync(channel_layer.group_send)(
            'graph_updates', {
                'type': 'send.data', 
                'data': {
                    'type': 'edge',
                    'obj': self.serializer_class(edge).data
                },
            }  # Customize this data as needed
        )


def graph_ui(request):
    vertices = Vertex.objects.all()
    edges = Edge.objects.all()
    return render(request, 'graph_ui.html', {
        'vertices': list(vertices.values()),
        'edges': list(edges.values())
    })
