from django.db import models

class Vertex(models.Model):
    name = models.CharField(max_length=50)

class Edge(models.Model):
    source = models.ForeignKey(Vertex, on_delete=models.CASCADE, related_name='outgoing_edges')
    target = models.ForeignKey(Vertex, on_delete=models.CASCADE, related_name='incoming_edges')