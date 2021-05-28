import java.awt.Color;
import java.awt.Graphics;
import java.awt.*;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

/** Página que documenta a nova API de comunicação serial 
 *  https://fazecast.github.io/jSerialComm/
 */

public class EspectroFotometroView extends JFrame {
	
	private static final boolean IMPRIME_DADOS_BRUTOS = true;
	private static final int TEMPO_INTEGRACAO = 1280;
	private static final int SAMPLES = 120;
	private static final int WAIT_TIME = 500; //em milisegundos	
	
	AuxThread auxThread = null;
	
	ImagePanel imgPanel = null;
	JButton btnReflectancia = null;						//Cria um objeto do tipo JButton com o nome btnReflectancia
	JButton btnCapture = null;
	JButton btnContinua = null;
	JButton btnStop = null;
	JButton btnIncreaseTI = null;	
	JButton btnDecreaseTI = null;
	JButton btnSave = null;		
	JButton btnGain = null;
	JButton btnBlack = null;
	JButton btnWhite = null;

	
	JLabel integTimeLabel = new JLabel("Integration Time: ");
	JLabel gainLabel = new JLabel("Gain:");
	JLabel batLabel = new JLabel("Tensão bateria(V):");
		
	JTextField integField = new JTextField();
	JTextField gainField = new JTextField();
	JTextField batField = new JTextField();
	
	EspectroFotometroView classeGrafica = null;
		
	Serial serial = null;
	float[] arrayDados = new float[128];
	float[] arrayBlack = new float[128];
	float[] arrayWhite = new float[128];
	float[] arrayReflectancia = new float[128];
	float[] arrayLambda = new float[128];
		
	float[] reflectanciaBranco = {42.01f,67.72f,81.59f,84.87f,85.89f,85.56f,87.02f,87.5f,88.36f,89.09f,89.89f,90.39f,90.99f,91.09f,91.45f,91.45f,91.54f,91.23f,90.88f,90.42f,90.23f,90.04f,89.8f,89.65f,89.68f,89.91f,89.77f,89.82f,90.1f,90.52f,90.75f};
	float[] px = {1.581f,7.749f,19.107f,27.279f,40.237f,43.375f,35.615f,22.467f,9.332f,1.763f,0.416f,4.043f,12.334f,25.468f,39.340f,55.124f,70.52f,84.649f,97.148f,99.195f,101.67f,92.332f,75.096f,53.929f,36.125f,21.471f,12.241f,6.689f,3.202f,1.387f,0.687f};
	float[] py = {0.166f,0.805f,1.999f,3.355f,6.512f,10.472f,15.103f,21.272f,29.239f,36.898f,50.39f,65.403f,79.829f,94.249f,100.438f,103.193f,99.73f,93057f,83.23f,68.944f,59.251f,47.308f,34.913f,23.612f,15.049f,8.611f,4.837f,2.616f,1.245f,0.537f,0,265f};
	float[] pz = {7.117f,35.625f,90.862f,134.661f,206.301f,233.408f,205.629f,151.341f,89.504f,45.189f,23.894f,12.074f,6.361f,3.285f,1.430f,0.416f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f,0.0f};
	float somapx = 1101.065f;
	float somapy = 1161.689f;
	float somapz = 1247.096f;
	int samples = 128;
	int integTime = 0;
	int sampleMaximo = 4095;
	int pixelsX=750;									     //Pixels eixo X
	int pixelsY=550;										//Pixels eixo Y
	int numDados=1;
	int feX=24;							//Fator de escala eixo X
	int feY=24;							//Fator de escala eixo Y
		
	public EspectroFotometroView() {
    	serial = new Serial();
		initPanel();
	}
	
	//Inicializa janela grafica
	private void initPanel(){
		this.setBounds(10, 10, 1100, 640);  //define tamanho da janela
		this.setBackground(Color.black);
		this.setTitle("Espectro Fotometro View");
		this.getContentPane().setLayout(null);
		
		classeGrafica = this;
    	auxThread = new AuxThread(classeGrafica);
    	
    //******************************************************Inicializa painel do gráfico*********************************************************************	
    	imgPanel = new ImagePanel();

		imgPanel.setBounds(10, 10, pixelsX, pixelsY);                	
		imgPanel.setBackground(Color.blue);
		this.add(imgPanel);
		
		//LABELS
		integTimeLabel.setBounds(760,15,160,25);
		integTimeLabel.setFont(new Font("Arial",Font.BOLD,18));		
		this.add(integTimeLabel);
		
		integField.setBounds(920, 15, 50, 20);
		integField.setText(""+TEMPO_INTEGRACAO);
		integField.setEditable(false);
		integField.setBackground(Color.WHITE);
		integField.setForeground(Color.BLACK);
		integField.setFont(new Font("Arial",Font.BOLD,18));		
		this.add(integField);
			
		gainLabel.setBounds(760,50,200,25);
		gainLabel.setFont(new Font("Arial",Font.BOLD,18));
		this.add(gainLabel);
				
		gainField.setBounds(920, 50, 50, 20);
		gainField.setFont(new Font("Arial",Font.BOLD,18));		
		gainField.setText("Low");
		this.add(gainField);
		
		
		batLabel.setBounds(760,85,200,25);
		batLabel.setFont(new Font("Arial",Font.BOLD,18));
		this.add(batLabel);
				
		batField.setBounds(920, 85, 50, 20);
		batField.setFont(new Font("Arial",Font.BOLD,18));		
		batField.setText("");
		this.add(batField);
		
		//CRIACAO DE BOTOES
		btnReflectancia = new JButton("Reflectancia");
		btnReflectancia.setBounds(990, 570, 110, 25);					//(x,y,deltax,deltay
		btnReflectancia.addActionListener(new ActionListener() 				
		{
            public void actionPerformed(ActionEvent et)
            {
            	serial.sendValue("5\n");									//Envia comando 5
            	trabalhandoComDadosDaCom();									//31 dados de reflectancia e lamdas em arrayDados e arrayLambda
            	setVisible(false);
            	setVisible(true);
            }             
        });		
		this.add(btnReflectancia);												//Adiciona io botao na tela
		
//*********************************************Botão Campture************************************************************************** 		
		btnCapture = new JButton("Capture");
		btnCapture.setBounds(10, 570, 80, 25);
		btnCapture.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent et) {
            	serial.sendValue("1\n");						//Envia comando "1"  p/ espectrofotometro
           		trabalhandoComDadosDaCom();									//Armazena intensidade em arrayDados  e lambda em arrayLambda
        	//	System.out.println("Dados de Reflectancia:");
        	//	for (int k=0;k<128;k++) {
        	//		arrayReflectancia[k] = (arrayDados[k]-arrayBlack[k])/(arrayWhite[k]-arrayBlack[k]);
        	//		System.out.println(arrayWhite[k] + " - " + arrayBlack[k] + " : " + arrayReflectancia[k]);            			
        		//}
            	// Imprime array independente se for de arquivo ou fotometro
            	setVisible(false);
            	setVisible(true);
            }             
        });		
		this.add(btnCapture);
		
		//*********************************************Botão Camptura Continua************************************************************************** 		
		btnContinua = new JButton("Captura Continua");
		btnContinua.setBounds(100, 570, 150, 25);
		btnContinua.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent et) {
            	if(auxThread.stop)
            		auxThread.stop = false;
            	else
            		auxThread.start();            
            }
		});
		this.add(btnContinua);
		
		btnStop = new JButton("Stop");
		btnStop.setBounds(260, 570, 80, 25);
		btnStop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent et) {
            	auxThread.stopando();
            }
		});
		this.add(btnStop);
		
		btnIncreaseTI = new JButton("Increase TI");
		btnIncreaseTI.setBounds(350, 570, 100, 25);
		btnIncreaseTI.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent et) {
            	serial.sendValue("2\n");
            	try { 
            		Thread.sleep(500);
            	}catch (Exception e) {
            	}
            	String texto = serial.readingFromFotometro(500);
            	StringTokenizer st = new StringTokenizer(texto,"\r");            	           	
            	integField.setText(st.nextToken());

            	try { 
            		Thread.sleep(1000);
            	}catch (Exception e) {
            	}

    	    	//Buscar dados da bateria
    	    	serial.sendValue("8\n");
    	    	String voltagem = serial.readingFromFotometro(500);
    	    	st = new StringTokenizer(voltagem,"\r");            	           	
    	    	batField.setText(st.nextToken());
            	            	
            }
		});
		this.add(btnIncreaseTI);		

		btnDecreaseTI = new JButton("Decrease TI");
		btnDecreaseTI.setBounds(460, 570, 100, 25);
		btnDecreaseTI.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent et) {
            	serial.sendValue("3\n");
            	try { 
            		Thread.sleep(500);
            	}catch (Exception e) {
            	}
            	String texto = serial.readingFromFotometro(500);
            	StringTokenizer st = new StringTokenizer(texto,"\r");            	
            	integField.setText(st.nextToken());
            	
    	    	//Buscar dados da bateria
    	    	serial.sendValue("8\n");
    	    	String voltagem = serial.readingFromFotometro(500);
    	    	st = new StringTokenizer(voltagem,"\r");            	           	
    	    	batField.setText(st.nextToken());            	
            }
		});
		this.add(btnDecreaseTI);
		
		btnSave = new JButton("Save");
		btnSave.setBounds(570, 570, 80, 25);
		btnSave.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent et) {
            	salvarArquivo(); 
            }
		});
		this.add(btnSave);

		btnGain = new JButton("Gain");
		btnGain.setBounds(660, 570, 80, 25);
		btnGain.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent et) {
            	serial.sendValue("4\n");
            	try { 
            		Thread.sleep(500);
            	}catch (Exception e) {
            	}
            	String texto = serial.readingFromFotometro(500);
            	StringTokenizer st = new StringTokenizer(texto,"\r");            	
            	gainField.setText(st.nextToken());
            	
    	    	//Buscar dados da bateria
    	    	serial.sendValue("8\n");
    	    	String voltagem = serial.readingFromFotometro(500);
    	    	st = new StringTokenizer(voltagem,"\r");            	           	
    	    	batField.setText(st.nextToken());            	
            	
            }
		});
		this.add(btnGain);
		
		btnBlack = new JButton("Black");
		btnBlack.setBounds(800, 570, 80, 25);
		btnBlack.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent et) {
        		trabalhandoComDadosDaCom();
        		arrayBlack=arrayDados;

        		// Imprime array independente se for de arquivo ou fotometro
            	setVisible(false);
            	setVisible(true);
            }             
        });		
		this.add(btnBlack);	
		
		btnWhite = new JButton("White");
		btnWhite.setBounds(890, 570, 80, 25);
		btnWhite.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent et) {
        		trabalhandoComDadosDaCom();
        		arrayWhite=arrayDados;            		
            	// Imprime array independente se for de arquivo ou fotometro
            	setVisible(false);
            	setVisible(true);
            }             
        });		
		this.add(btnWhite);	
	}
	
	/** Salva em arquivo */
	private void salvarArquivo(){
		try {
			Calendar cal = Calendar.getInstance();
			Date data = cal.getTime();
			String diaDeHoje = data.getDate()+"-"+ (data.getMonth()+1) + "-" + (data.getYear()+1900) + "-" + data.getHours() + "-" + data.getMinutes(); 
			FileWriter fis = new FileWriter("espectro" + diaDeHoje +".txt");
			BufferedWriter writter = new BufferedWriter(fis);
			for (int i=0;i<128;i++) {				
				writter.write(Float.toString(arrayDados[i])+"\n");
			}
			writter.close();
			fis.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	/******************************************** Trata os dados vindos da porta COM ****************************/
	private void trabalhandoComDadosDaCom(){
    	
    	//serial.sendValue("1\n");						//O comando esta sendo enviando pelo botão antes de chamar esta rotina.
    	try { 
    		Thread.sleep(500);
    	}catch (Exception e) {
    	}
    	String texto = serial.readingFromFotometro(2000);   //Captura dados enviados pelo espectro 
    	if (IMPRIME_DADOS_BRUTOS) {
    		System.out.println(texto);
    	}
    	        
    	StringTokenizer st = new StringTokenizer(texto,"\r");
    	numDados = st.countTokens();							//Determina o numero de linhas, ou seja numero de dados 
    	System.out.println("Número de linhas: "+numDados);    			//Pode ser 128 ou 31
    	arrayDados = new float[numDados];
    	arrayLambda = new float[numDados];
    	for (int i=0; i<numDados-1; i++) {
    		String value = st.nextToken().trim();
    		StringTokenizer newToken = new StringTokenizer(value," ");
    		//int temp = newToken.countTokens();
    		//Primeiro Token
    		try {
	    		arrayDados[i] = Float.parseFloat(newToken.nextToken().trim());   //Intensidades em arrayDados
	    		String floatValue = newToken.nextToken().trim();
	    		arrayLambda[i] = Float.parseFloat(floatValue);					//Comprimentos de onda em array Lambda
    		}catch(Exception e) {
    			System.out.println("Erro lendo dado: " + i);
    			e.printStackTrace();
    		}
    	}	
	}
	
	private void imprimeArrayDados(){
		System.out.println("Apresentando array lido.");
		for(int j=0;j<arrayDados.length;j++) {
			System.out.println(j+" - "+arrayDados[j]);
		}
		System.out.println("FIM ARRAYDADOS.");		
	}
		
	//*************************************Main do software******************8
	public static void main(String[] args) {
		System.out.println("   ESPECTRO FOTOMETRO VERSÃO 2021 ");
		System.out.println("--------------------------------");	
		
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
    		EspectroFotometroView view = new EspectroFotometroView();
    		view.setVisible(true);
    		
        } catch (Exception e) {
            System.out.println("Erro inicializando janela");
            e.printStackTrace();
        }        
	}
	class ImagePanel extends Panel {		
		public Image myimg = null;
			
		private int XOffSet = 10;
		private int YOffSet = 50;		
		private int YOffSetDados = 450;		
	
		private int XMin = 500;
		private int minValue = 500;
		//private int maxValue = 50;
		private int XOffSet1 = 300;
		boolean graph = false;
	    
	    public ImagePanel() {
	      setLayout(null);
	      setSize(320,850);
	    }
	    	    
	    public void paint(Graphics g) {
	        
	        //Linhas de maximo e minimo
	        g.setColor(Color.WHITE);
	        feX=pixelsX/numDados;
	        feY=pixelsY/numDados;
	        paintAxis(g);					//Cria os eixos X, Y e linhas hprizontais e verticais
        	paintGraph(g);					//Cria o gráfico
        	paintNumbers(g);				//Cria números dos esixos

	    }
	    
	    /************************************* Imprime Eixos e Linhas Verticais  ******************************/
	    private void paintAxis(Graphics g) 
	    {
	    	g.setColor(Color.yellow);
	    	g.drawLine(XOffSet, minValue, XOffSet + 700, minValue);  //Eixo X
	    	g.drawLine(XOffSet, minValue, XOffSet, YOffSet);  //Eixo Y
	    	g.setColor(Color.white);
	    	// Linhas verticas de 20 em 20
	    	for(int i=0; i<pixelsX; i=i+50)
	    	{
	    		g.drawLine(XOffSet+i, YOffSet, XOffSet+i, YOffSet + 650 );  //Eixo Y
	    	}
	    	// Linhas horizontais de 20 em 20
	    	for(int i=0; i<pixelsY; i=i+50)
	    	{
	    		g.drawLine(XOffSet, YOffSet+i, XOffSet+700, YOffSet+i );  //Eixo Y
	    	}
	    }
	    
	    /********************************** Faz o gráfico na parte inferior da janela **************************/
	    private void paintGraph (Graphics g) 
	    {
	    	//feX=pixelsX/numDados;
		    for (int i=0; i < arrayDados.length-1; i++) 
		    	{
			    	g.setColor(Color.WHITE);
			   	    g.drawLine(XOffSet+(i*feX),YOffSetDados-(int)arrayDados[i]/10, XOffSet+((i+1)*feX) ,YOffSetDados-(int)arrayDados[i+1]/10);			   	    
		    	}	    	
	    }
	    
	    /** Normaliza valor fazendo regra de tres */
	    private float normalize(float data) {
	    	float result = 0;
	    	result = (data * minValue) / sampleMaximo;	    	
	    	return result;
	    }
	    
	    /****************************** Coloca numeros no grafico ***************/
	    private void paintNumbers(Graphics g){
	    	//Pegando cor atual
	    	System.out.println("Entrei aqui");
	    	if(arrayDados != null) {
		    	Color actualColor = g.getColor();
		    	
		    	//Setando configuracao para pintar os numeros
		    	g.setColor(Color.WHITE);
		  	int delta;
		  	if (numDados > 70) 
		  		delta=10;
		  	else
		  		delta=2;
		  	
		    for(int i=0; i<numDados; i=i+delta) 
		    	{
		    		 g.drawString(""+arrayLambda[i],XOffSet+i*feX+5, YOffSet+470 );  	
		    	//	 g.drawString(""+arrayDados[i],XOffSet, YOffSet+feY*i );  
		    		
		    	}
		    
		    for(int i=0; i<101; i=i+20) 
			    	{
			    		  g.drawString(""+String.valueOf(i),XOffSet,470-4*i );  	    		 
			    	}
			    	
		    	//devolvendo a cor que estava sendo usada
		    	g.setColor(actualColor);
	    	}
	    	
	    }
	    
	  }	
	
	class AuxThread extends Thread {
		private EspectroFotometroView classePai;
		private boolean stop = false;
		
		public AuxThread(EspectroFotometroView classePai_) {
			classePai = classePai_;
		}
		
		public void run() {
        	//Faz leitura SAMPLES vezes e espera WAIT_TIME em casa leitura
        	while(true) {
        		if(!stop) {
		            try { 
	            		Thread.sleep(200);
	            	}catch (Exception e) { }
	            	trabalhandoComDadosDaCom();
	            	classePai.setVisible(false);
	            	classePai.setVisible(true);
        		} else {
		            try { 
	            		Thread.sleep(500);
	            	}catch (Exception e) { }
        		}
        	}
		}
		
		//Para leitura
		public void stopando(){
			stop=true;
		}
	}
	
}
