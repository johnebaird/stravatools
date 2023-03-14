from django.urls import path

from . import views

app_name = "defaultbikes"
urlpatterns = [
    # / 
    path('defaultbikes/', views.defaultbikes, name='defaultbikes'), 
    
    ]