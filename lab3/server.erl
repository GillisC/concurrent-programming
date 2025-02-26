-module(server).
-export([start/1, stop/1]).

%STATE is the list of channel Atoms 

% Start a new server process with the given name
% Do not change the signature of this function.
start(ServerAtom) ->
    Pid = genserver:start(ServerAtom, [], handle),
    Pid. 
 
% Stop the server process registered to the given name,
% together with any other associated processes
stop(ServerAtom) ->
    genserver:stop(ServerAtom).

% This is F we send into genserver
handle(State, {join, ChannelAtom, From}) ->
    case lists:member(ChannelAtom, State) of 
        true -> NewState = lists:append(State, [ChannelAtom])
    NewState = lists:append(State, [ChannelAtom]),
    {reply, ok, NewState}.
    
    
    