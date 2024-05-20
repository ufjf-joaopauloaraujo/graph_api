# graph_api/urls.py

from rest_framework import routers
from .views import graph_ui
from django.urls import path, include
from .views import (
    VertexViewSet,
    EdgeViewSet,
)

router = routers.DefaultRouter()
router.register(r'vertices', VertexViewSet)
router.register(r'edges', EdgeViewSet)

urlpatterns = [
    path('api/', include(router.urls)),
    path('', graph_ui, name='graph-ui'),
]
