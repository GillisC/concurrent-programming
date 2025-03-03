-module(channel).
-export([stop/1, start/1, handle/2]).


start(ChannelAtom) ->
    Pid = genserver:start(ChannelAtom, [], fun handle/2),
    Pid. 

stop(ChannelAtom) ->
    genserver:stop(ChannelAtom).

handle(Users, {join, Client}) ->
    io:format("~p~n", [here]),
    case lists:member(Client, Users) of 
        true  -> 
            {reply, ok, Client};
        false -> 
            {reply, ok, [Client | Users]}
    end. 
    

    
    
    