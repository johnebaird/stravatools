# Generated by Django 4.1.5 on 2023-03-02 14:37

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('muting', '0002_alter_muting_activitytype'),
    ]

    operations = [
        migrations.AlterField(
            model_name='muting',
            name='muteall',
            field=models.BooleanField(),
        ),
    ]
