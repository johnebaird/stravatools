# Generated by Django 4.1.5 on 2023-03-01 18:29

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('maintenance', '0001_initial'),
    ]

    operations = [
        migrations.AlterField(
            model_name='reminder',
            name='last_sent',
            field=models.IntegerField(blank=True, null=True),
        ),
    ]
