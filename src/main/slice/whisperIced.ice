
#pragma once

["java:package:jeremie.lohyer"]
module whisperIced
{
    sequence<byte> dataArray;

    interface SpeechReceiver
    {
        void addClient(string address, string port);
        void prepareUpload(int nbBlocs);
        void upload(int blocId, dataArray data);
    };
    interface TextSender
    {
        void getTranscription(string transcription);
        void responseGetCompletion(int complete);
    }
}