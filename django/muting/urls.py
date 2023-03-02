from django.urls import path

from . import views

urlpatterns = [
    path('muting', views.muting, name='muting'),
]
