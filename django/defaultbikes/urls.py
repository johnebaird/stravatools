from django.urls import path

from . import views

urlpatterns = [
    # / 
    path('defaultbikes/', views.defaultbikes, name='defaultbikes'), 
    
    ]