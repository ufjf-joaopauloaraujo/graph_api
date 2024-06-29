from django.db import models

class Vertex(models.Model):
    id = models.AutoField(primary_key=True)
    name = models.CharField(max_length=50)
    color = models.CharField(max_length=7, default="white")

class Edge(models.Model):
    id = models.AutoField(primary_key=True)
    source = models.ForeignKey(Vertex, on_delete=models.CASCADE, related_name='outgoing_edges')
    target = models.ForeignKey(Vertex, on_delete=models.CASCADE, related_name='incoming_edges')
    name = models.CharField(max_length=150, blank=True)
    description = models.CharField(max_length=50, blank=True)
    
    def save(self, *args, **kwargs):
        self.name = f"{self.source.name},{self.description},{self.target.name}"
        super(Edge, self).save(*args, **kwargs)
