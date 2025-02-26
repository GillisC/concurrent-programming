-module(channel).
-export([start/1]).


start(ChannelAtom) ->
    Pid = genserver:start(ChannelAtom, [], fun(State, _Data) -> {reply, "HI", State} end),
    Pid. 

stop(ChannelAtom) ->
    genserver:stop(ChannelAtom).

handle(User, {create, ChannelAtom}) ->
    {ok, ChannelAtom}
    
    