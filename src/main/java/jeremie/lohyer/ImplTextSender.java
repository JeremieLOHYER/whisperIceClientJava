package jeremie.lohyer;//
// Copyright (c) ZeroC, Inc. All rights reserved.
//

import com.zeroc.Ice.Current;
import jeremie.lohyer.whisperIced.TextSender;

import java.util.function.Consumer;

public class ImplTextSender implements TextSender
{
    public Consumer<String> getTranscriptionCallBack = (s) -> System.out.println("transcribed text : " + s);

    public Consumer<Integer> getCompletionCallBack = (epoch) -> System.out.println("upload completion : " + epoch);

    public void setGetTranscriptionCallBack(Consumer<String> callBack) {
        getTranscriptionCallBack = callBack;
    }
    public void setGetCompletionCallBack(Consumer<Integer> callBack) {
        getCompletionCallBack = callBack;
    }

    @Override
    public void getTranscription(String transcription, Current current) {
        getTranscriptionCallBack.accept(transcription);
    }

    @Override
    public void responseGetCompletion(int complete, Current current) {
        getCompletionCallBack.accept(complete);
    }
}
