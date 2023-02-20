# Generated by Django 4.1.5 on 2023-02-08 16:15

from django.conf import settings
from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    dependencies = [
        migrations.swappable_dependency(settings.AUTH_USER_MODEL),
        ('main', '0001_initial'),
    ]

    operations = [
        migrations.CreateModel(
            name='Profile',
            fields=[
                ('id', models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('bearer', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, to='main.bearer')),
                ('indoor_bike', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, related_name='indoor_bike', to='main.bike')),
                ('outdoor_bike', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, related_name='outdoor_bike', to='main.bike')),
                ('user', models.OneToOneField(on_delete=django.db.models.deletion.CASCADE, to=settings.AUTH_USER_MODEL)),
            ],
        ),
        migrations.DeleteModel(
            name='User',
        ),
    ]