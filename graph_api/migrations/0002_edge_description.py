# Generated by Django 5.0.6 on 2024-06-07 00:03

import django.utils.timezone
from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('graph_api', '0001_initial'),
    ]

    operations = [
        migrations.AddField(
            model_name='edge',
            name='description',
            field=models.CharField(default=django.utils.timezone.now, max_length=50),
            preserve_default=False,
        ),
    ]
