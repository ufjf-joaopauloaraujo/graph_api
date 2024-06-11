from django.db import models

class Vertex(models.Model):
    name = models.CharField(max_length=50, primary_key=True)

class Edge(models.Model):
    source = models.ForeignKey(Vertex, on_delete=models.CASCADE, related_name='outgoing_edges')
    target = models.ForeignKey(Vertex, on_delete=models.CASCADE, related_name='incoming_edges')
    name = models.CharField(max_length=150, primary_key=True, blank=True)
    description = models.CharField(max_length=50, blank=True)
    
    def save(self, *args, **kwargs):
        self.name = f"{self.source.name},{self.description},{self.target.name}"
        super(Edge, self).save(*args, **kwargs)
