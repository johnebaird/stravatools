# Generated by Django 4.1.5 on 2023-02-24 18:57

from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    dependencies = [
        ('defaultbikes', '0005_alter_defaultbike_indoor_bike_and_more'),
        ('main', '0008_alter_profile_bearer'),
    ]

    operations = [
        migrations.AlterField(
            model_name='profile',
            name='bearer',
            field=models.OneToOneField(blank=True, null=True, on_delete=django.db.models.deletion.SET_NULL, to='main.bearer'),
        ),
        migrations.AlterField(
            model_name='profile',
            name='defaultbikes',
            field=models.OneToOneField(blank=True, null=True, on_delete=django.db.models.deletion.SET_NULL, to='defaultbikes.defaultbike'),
        ),
    ]