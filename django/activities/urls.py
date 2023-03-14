from django.urls import path

from . import views

urlpatterns = [
    path('activities/', views.activities, name='activities'),
    path('activities/<int:id>', views.activitydetail, name='activitydetail'),
    ]
