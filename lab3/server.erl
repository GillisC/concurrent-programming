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
    genserver:request(ServerAtom, stop),  %Calls genserver to get the current state
    genserver:stop(ServerAtom).

handle(Channels, stop) -> 
    lists:foreach(fun(X) ->
          channel:stop(X) end, Channels), %Stops all channels before stopping
    {reply, ok, []};

% This is F we send into genserver
handle(Channels, {join, Channel, Client}) ->
    ChannelAtom = list_to_atom(Channel), %Channel is sent a string (list of chars)
    NewState = case lists:member(ChannelAtom, Channels) of 
        false -> 
            channel:start(ChannelAtom),     
            [ChannelAtom | Channels];
        true -> 
            Channels %Just return the state
    end,
    Result = genserver:request(ChannelAtom, {join, Client}),
    {reply, Result, NewState}.