# Generated by Django 4.1.5 on 2023-03-09 18:07

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('main', '0004_logging_application'),
    ]

    operations = [
        migrations.AddField(
            model_name='profile',
            name='distance',
            field=models.CharField(choices=[('km', 'km'), ('miles', 'miles')], default='km', max_length=100),
        ),
    ]
