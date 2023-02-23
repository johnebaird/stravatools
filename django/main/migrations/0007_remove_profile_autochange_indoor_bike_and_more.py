# Generated by Django 4.1.5 on 2023-02-23 16:29

from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    dependencies = [
        ('defaultbikes', '0001_initial'),
        ('main', '0006_alter_bearer_expires_at'),
    ]

    operations = [
        migrations.RemoveField(
            model_name='profile',
            name='autochange_indoor_bike',
        ),
        migrations.RemoveField(
            model_name='profile',
            name='autochange_outdoor_bike',
        ),
        migrations.RemoveField(
            model_name='profile',
            name='indoor_bike',
        ),
        migrations.RemoveField(
            model_name='profile',
            name='outdoor_bike',
        ),
        migrations.AddField(
            model_name='profile',
            name='defaultbikes',
            field=models.OneToOneField(blank=True, null=True, on_delete=django.db.models.deletion.CASCADE, to='defaultbikes.defaultbikes'),
        ),
        migrations.DeleteModel(
            name='Bike',
        ),
    ]
