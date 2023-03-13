from django.contrib import admin
from .models import Bearer, Profile, Logging
# Register your models here.

admin.site.register(Profile)
admin.site.register(Bearer)
admin.site.register(Logging)
