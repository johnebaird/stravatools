# Generated by Django 4.1.5 on 2023-03-02 15:47

from django.db import migrations


class Migration(migrations.Migration):

    dependencies = [
        ('main', '0001_initial'),
    ]

    operations = [
        migrations.RenameField(
            model_name='logging',
            old_name='defaultbikes',
            new_name='profile',
        ),
    ]