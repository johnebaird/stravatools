from django.http import HttpResponse
from django.shortcuts import render, redirect

# Create your views here.

def index(request):
    return render(request, 'main/index.html')
    
def login(request):
    return render(request, 'main/login.html')

def register(request):
    if 'code' in request.session:
        return render(request, 'main/register.html')
    else:
        return redirect(index)

def exchange_token(request):
    request.session['code'] = request.GET['code']
    return redirect(register)

