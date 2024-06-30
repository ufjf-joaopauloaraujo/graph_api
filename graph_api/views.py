# graph_api/views.py

from django.shortcuts import render
from rest_framework import viewsets, status, response
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
                    'operation': 'create',
                    'obj': self.serializer_class(vertex).data
                },
            }
        )
    
    def perform_update(self, serializer):
        # Save the new vertex
        vertex = serializer.save()

        # Send the updated graph data to connected clients
        channel_layer = get_channel_layer()
        async_to_sync(channel_layer.group_send)(
            'graph_updates', {
                'type': 'send.data', 
                'data': {
                    'type': 'vertex',
                    'operation': 'update',
                    'obj': self.serializer_class(vertex).data
                },
            }
        )
        
    def perform_destroy(self, serializer):
        # Save the new vertex
        vertex = serializer
        serializer.delete()
        vertex.id = self.kwargs["pk"]

        # Send the updated graph data to connected clients
        channel_layer = get_channel_layer()
        async_to_sync(channel_layer.group_send)(
            'graph_updates', {
                'type': 'send.data', 
                'data': {
                    'type': 'vertex',
                    'operation': 'delete',
                    'obj': self.serializer_class(vertex).data
                },
            }
        )
    
    def destroy(self, request, *args, **kwargs):
        if kwargs['pk'] == 'all':
            for instance in self.get_queryset():
                instance.delete()
                
                # Send the updated graph data to connected clients
                channel_layer = get_channel_layer()
                async_to_sync(channel_layer.group_send)(
                    'graph_updates', {
                        'type': 'send.data', 
                        'data': {
                            'type': 'all',
                            'operation': 'delete'
                        },
                }
            )
            return response.Response(status=status.HTTP_204_NO_CONTENT)
        return super().destroy(request, *args, **kwargs)


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
                    'operation': 'create',
                    'obj': self.serializer_class(edge).data
                },
            }
        )
    
    def perform_update(self, serializer):
        # Save the new edge
        edge = serializer.save()

        # Send the updated graph data to connected clients
        channel_layer = get_channel_layer()
        async_to_sync(channel_layer.group_send)(
            'graph_updates', {
                'type': 'send.data', 
                'data': {
                    'type': 'edge',
                    'operation': 'update',
                    'obj': self.serializer_class(edge).data
                },
            }
        )
        
    def perform_destroy(self, serializer):
        # Save the new edge
        edge = serializer
        serializer.delete()
        edge.id = self.kwargs["pk"]

        # Send the updated graph data to connected clients
        channel_layer = get_channel_layer()
        async_to_sync(channel_layer.group_send)(
            'graph_updates', {
                'type': 'send.data', 
                'data': {
                    'type': 'edge',
                    'operation': 'delete',
                    'obj': self.serializer_class(edge).data
                },
            }
        )


def graph_ui(request):
    vertices = Vertex.objects.all()
    edges = Edge.objects.all()
    return render(request, 'graph_ui.html', {
        'vertices': list(vertices.values()),
        'edges': list(edges.values())
    })
