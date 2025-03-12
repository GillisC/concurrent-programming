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
    case whereis(ServerAtom) of
        undefined -> 
            io:format("Server already stopped.~n"),
            {error, server_not_running};
        Pid when is_pid(Pid) ->
            io:format("Stopping server: ~p~n", [Pid]),
            
            % Send an async stop request
            genserver:stop(ServerAtom),

            % Wait for the server to actually stop
            timer:sleep(100),
            ok
    end.

handle(Channels, stop) ->
    lists:foreach(fun(X) -> channel:stop(X) end, Channels);

handle(Channels, stop) ->
    lists:foreach(
      fun(X) ->
          io:format("Stopping channel: ~p~n", [X]),
          channel:stop(X)
      end,
      Channels
    ),
    {reply, ok, []};

% This is F we send into genserver
handle(Channels, {join, ChannelAtom, Client}) ->
    NewState = case lists:member(ChannelAtom, Channels) of 
        false -> 
            channel:start(ChannelAtom),     
            [ChannelAtom | Channels];
        true -> 
            Channels
    end,
    Result = genserver:request(list_to_atom(ChannelAtom), {join, Client}),
    {reply, Result, NewState}.
