-module(channel).
-export([stop/1, start/1, handle/2]).


start(ChannelAtom) ->
    Pid = genserver:start(ChannelAtom, [], fun handle/2),
    Pid. 

stop(ChannelAtom) ->
    genserver:stop(ChannelAtom).

handle(Users, {join, Client}) ->
    case lists:member(Client, Users) of 
        true  -> 
            {reply, {error, user_already_joined, "user already joined" }, Users};
        false -> 
            {reply, ok, [Client | Users]}
    end;

handle(Users, {leave, Client}) ->
    case lists:member(Client, Users) of 
        false -> 
            {reply, {error, user_not_joined, "Client not member!"}, Users};
        true -> 
            {reply, ok, lists:delete(Client, Users)} %Make this quit 
    end;

handle(Users, {message_send, Channel, Msg, Client, Nick}) ->
    case lists:member(Client, Users) of
        false ->
            {reply, {error, user_not_joined, "User not a member!"}, Users};
        true ->
        lists:foreach(fun(X) -> 
            spawn(fun() ->
                genserver:request(X, {message_receive, Channel, Nick, Msg}) 
            end)
        end, 
        lists:delete(Client, Users)),
        {reply, ok, Users}    
    end.

% get_string_from_pid() -> This was pro coding but took to long for 500ms
%     atom_to_list(element(2,process_info(self(), registered_name))).