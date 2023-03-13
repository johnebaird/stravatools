from django.contrib import admin
from .models import DefaultBike, Bike
# Register your models here.

admin.site.register(DefaultBike)
admin.site.register(Bike)