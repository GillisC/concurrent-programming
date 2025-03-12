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
    genserver:request(ServerAtom, stop),
    genserver:stop(ServerAtom).

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
    io:format("hello, ~p~n", [Channels]),
    NewState = case lists:member(ChannelAtom, Channels) of 
        false -> 
            channel:start(ChannelAtom),     
            [ChannelAtom | Channels];
        true -> 
            Channels
    end,
    Result = genserver:request(list_to_atom(ChannelAtom), {join, Client}),
    {reply, Result, NewState};

handle(Channels, {leave, ChannelAtom, Client}) ->
    io:format("~p~n", [Channels]),
    NewState = case lists:member(ChannelAtom, Channels) of 
        true -> 
            lists:delete(ChannelAtom, Channels);
        false -> 
            Channels
    end,
    Result = genserver:request(list_to_atom(ChannelAtom), {leave, Client}),
    {reply, Result, NewState};


handle(Channels, {message_send, ChannelAtom, Msg, Nick, Client}) ->
    
    % Check if the client is part of the channel
    case lists:member(ChannelAtom, Channels) of
        false ->
            {reply, {error, user_not_joined, "User not a member!"}, Channels};
        true ->
            % Send the message to the channel for broadcasting
            Result = genserver:request(list_to_atom(ChannelAtom), {broadcast_message, list_to_atom(ChannelAtom), Msg, Nick, Client}),
            {reply, Result, Channels}
    end.
