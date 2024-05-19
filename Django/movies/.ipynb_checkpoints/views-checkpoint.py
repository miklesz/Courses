from django.shortcuts import render
from datetime import datetime  # NOWE
from movies.models import Movie

# Create your views here.
# def hello_world(request):
#     return render(request, template_name="hello.html")  # NOWE

# Create your views here.
def hello_world(request):
    our_context = {"time": datetime.now()}  # NOWE
    return render(
        request, 
        template_name="hello.html", 
        context=our_context
    )  # NOWE

def list_movies(request):
    movies = Movie.objects.all()
    return render(
        request, 
        template_name="movie_list.html", 
        context={"movies": movies}
    )

