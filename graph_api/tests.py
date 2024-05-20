from django.test import TestCase

# graph_api/tests.py

from django.test import TestCase
from rest_framework.test import APIClient
from .models import Vertex

class VertexAPITestCase(TestCase):
    def setUp(self):
        self.client = APIClient()

    def test_create_vertex(self):
        data = {'name': 'Vertex A'}
        response = self.client.post('/graph_api/api/vertices/', data)
        self.assertEqual(response.status_code, 201)
        self.assertEqual(Vertex.objects.count(), 1)
        self.assertEqual(Vertex.objects.first().name, 'Vertex A')
