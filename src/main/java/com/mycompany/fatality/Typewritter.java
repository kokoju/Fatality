package com.mycompany.oceanicwars;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JTextArea;
import javax.swing.Timer;

/**
 *
 * @author kokoju
 */
public class Typewritter {  // Función encargada de mostrar el texto de manera gradual, en lugar de imprimirlo en su totalidad
    private static Timer currentTimer = null;  // Variable para almacenar un Timer al mostrar elementos 
    
    public static void typeText(JTextArea txaLabel, String texto, int delay, boolean preservar_anterior) {  // Recibe un TextArea, además de el texto y el 'delay' que queremos
        if (currentTimer != null && currentTimer.isRunning()) {  // Si hay un Timer y este está corriendo (si se tocó una casilla de manera sumamente reciente)
            currentTimer.stop();  // Se detiene su timer
        }
        
        if (!preservar_anterior) {
            txaLabel.setText("");  // Quita el texto anterior
        }
        currentTimer = new Timer(delay, null);  // Establece un Timer con cierto delay, el cuál da hace un tick cada 'delay' segundos

        currentTimer.addActionListener(new ActionListener() {  // Si el timer hace un tick
            int i = 0;  // Variable 'i' que lleva el índice del texto revisado: no se reinicia cada tick, solo se crea una vez

            @Override
            public void actionPerformed(ActionEvent e) {  // Esta función si se repite cada tick, y imprime una letra más mientras siga habiendo
                if (i < texto.length()) {
                    txaLabel.setText(txaLabel.getText() + texto.charAt(i));  // Escribe en el texto que ya había el nuevo char 
                    i++;
                } else {  // Si no quedan más letras, el timer se detiene y la función termina
                    currentTimer.stop();
                }
            }
        });

        currentTimer.start();  // Inicio del timer
    }
    
    
    public static void cancel() {  // Método para cancelar una corrida del timer manualmente, en caso de ocuparse (static para acceder con facilidad)
        if (currentTimer != null) {  // Si hay un timer
            currentTimer.stop();  // Se detiene
        }
    }
}
