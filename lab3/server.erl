-module(server).
-export([start/1, stop/1, handle/2]).

%STATE is the list of channel Atoms 

% Start a new server process with the given name
% Do not change the signature of this function.
start(ServerAtom) ->
    Pid = genserver:start(ServerAtom, [], fun handle/2),
    Pid. 
 
% Stop the server process registered to the given name,
% together with any other associated processes
stop(ServerAtom) ->
    genserver:stop(ServerAtom).

% This is F we send into genserver
handle(Channels, {join, ChannelAtom, Client}) ->
    NewState = case lists:member(ChannelAtom, Channels) of 
        false -> 
            channel:start(list_to_atom(ChannelAtom)),     
            [ChannelAtom | Channels];
        true -> 
            Channels
    end,
    Result = genserver:request(list_to_atom(ChannelAtom), {join, Client}),
    {reply, Result, NewState}.