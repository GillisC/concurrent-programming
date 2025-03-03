-module(channel).
-export([start/1]).


start(ChannelAtom) ->
    Pid = genserver:start(ChannelAtom, [], handle),
    Pid. 

stop(ChannelAtom) ->
    genserver:stop(ChannelAtom).

handle(Users, {join, Client}) ->
    case lists:member(Client, Users) of 
        true  -> 
            {reply, ok, Client}
        false -> 
            {reply, ok, [Client | Users]}
    end. 

    
    
    