from django.http import HttpResponse
from django.shortcuts import render

# Create your views here.

def index(request):
    return render(request, 'main/index.html')
    
def login(request):
    return render(request, 'main/login.html')

def register(request):
    return render(request, 'main/register.html')

def exchange_token(request):
    return HttpResponse(request.GET.values())


