from django.urls import path

from . import views

urlpatterns = [
    # / 
    path('', views.index, name='index'), 
    # /register
    path ('register/', views.register, name='register'),
    # /exchange_token
    path ('exchange_token/', views.exchange_token, name='exchange token'),
    # /activities
    path('activities/', views.activities, name='activities'),
]
