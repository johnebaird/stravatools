from django.urls import path

from . import views

urlpatterns = [
    # / 
    path('', views.index, name='index'), 
    path ('accounts/register/', views.register, name='register'),
    path ('accounts/profile', views.profile, name='profile'),
    path ('exchange_token/', views.exchange_token, name='exchange_token'),
    ]
