# graph_api/serializers.py

from rest_framework import serializers
from .models import Vertex, Edge

class VertexSerializer(serializers.ModelSerializer):
    class Meta:
        model = Vertex
        fields = '__all__'

class EdgeSerializer(serializers.ModelSerializer):
    class Meta:
        model = Edge
        fields = '__all__'
