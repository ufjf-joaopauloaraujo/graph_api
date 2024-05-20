import json

from asgiref.sync import async_to_sync
from channels.generic.websocket import WebsocketConsumer

class GraphConsumer(WebsocketConsumer):
    def connect(self):
        
        async_to_sync(self.channel_layer.group_add)(
            'graph_updates', self.channel_name
        )
        
        self.accept()
        # self.channel_layer.group_add('graph_updates', self.channel_name)

    def disconnect(self, close_code):
        async_to_sync(self.channel_layer.group_discard)(
            'graph_updates', self.channel_name
        )
        # self.channel_layer.group_discard('graph_updates', self.channel_name)

    def receive(self, text_data):
        # Handle incoming WebSocket messages (if needed)
        pass

    def send_data(self, event):
        graph_data = event['data']
        self.send(text_data=json.dumps({"data": graph_data}))
