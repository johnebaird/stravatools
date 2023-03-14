from django.urls import path

from . import views

app_name = 'activities'
urlpatterns = [
    path('activities/', views.activities, name='activities'),
    path('activities/<int:id>', views.activitydetail, name='activitydetail'),
    ]
