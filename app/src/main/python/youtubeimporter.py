import os.path
from pytube import YouTube

def main(youtube_link,media_path):

    #For Test purposes
    #media_path="/storage/emulated/0/Download"
    #Name your directory here.
    #directory = "youtube_music"
    #path = os.path.join(media_path,directory)
    #End Test Area

    if not os.path.isdir(media_path):
        os.mkdir(media_path)

    video = YouTube(youtube_link)

    #download stream
    out_file = video.streams.filter(only_audio=True).first().download(media_path)

    #Convert the format to mp3
    base, ext = os.path.splitext(out_file)
    new_file = base + ".mp3"
    os.rename(out_file, new_file)

    return video.title + " has been successfully downloaded."
