/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package game.client.network;

import game.common.dto.GameResponse;

public interface ResponseCallback {
    
/*
    invocato alla ricezione di un messaggio dal server
    */
    void onResponse(GameResponse response);

    void onError(String errorMessage);
}
