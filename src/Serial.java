import com.fazecast.jSerialComm.*;

public class Serial {
	
	SerialPort porta = null;
	
	public Serial() {
		//construtor vazio que pega a primeira COM encontrada e abre a porta para uso
		porta = SerialPort.getCommPorts()[0];
		porta.openPort();
		System.out.println("Porta ABERTA: "+porta.getPortDescription());
	}
	
	/** Construtor. Inicia comunicacao com porta serial passada por parametro, cria streams e seta parametros 
	 * de comunicacao com porta serial
	 * 
	 * Cleo. 26.08.2008
	 */
	public Serial(String idPorta) {
		//CommPortIdentifier cp = null;
		//SerialPort porta = null;
		try {
			
			//cp = CommPortIdentifier.getPortIdentifier(idPorta);			
		} catch (Exception e) {
			System.out.println("Porta " + idPorta + " desconhecida");
			e.printStackTrace();	
			System.exit(1);
		}
		
		System.out.println("Abrindo porta serial " + idPorta);
		try {
			//porta = (SerialPort)cp.open("SComm",1000);
		} catch (Exception e){
			System.out.println("ERRO. Porta continua em uso");
			e.printStackTrace();	
			System.exit(1);
		}
		
		try {
			System.out.println("Criando streams");		
			//entrada = porta.getInputStream();
			//saida = porta.getOutputStream();
		} catch (Exception e){
			System.out.println("ERRO. Criando Streams");
			e.printStackTrace();
		}

		try{
			System.out.println("Setando parametros de configuração da Porta");
			//System.out.println("   BaudRate: " + BAUD_RATE);			
			//porta.setSerialPortParams(BAUD_RATE, porta.DATABITS_8, porta.STOPBITS_1, porta.PARITY_NONE);
		} catch (Exception e){
			System.out.println("ERRO. Setando parametros de configuracao da porta");
			e.printStackTrace();
		}
		
	}
		
	//função que fecha a conexão
	public void closePort(){
		try {
			porta.closePort();
		} catch (Exception e) {
			System.out.println("ERRO AO FECHAR PORTA. STATUS: " + e );
		}
	}
	
	/**
	 *  Verifica as portas disponíveis
	 */
	public static void checkPorts(){
		System.out.println("\nApresentando portas de comunicacao disponíveis: ");

		SerialPort[] listaSerialPort = SerialPort.getCommPorts();
		for(int i=0; i<listaSerialPort.length; i++) {
			System.out.println("Porta encontrada: "+listaSerialPort[i].getDescriptivePortName());
		}
	}
	
	/**
	 *   Enviando 1 para iniciar comunicacao com espectoFotometro
	 */
	public void sendValue(String value) {		
		try { 
			porta.writeBytes(value.getBytes(), value.length());
		} catch (Exception e) {
			System.out.println("erro escrevendo na porta serial");
			e.printStackTrace();
		}
	}
	
	/** Lendo String a partir do Espectofotometro */
	public String readingFromFotometro(int tempoEspera) {
		String result = "";
		porta.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, tempoEspera, 0);
		
		try {
		   while (true) {
		      byte[] readBuffer = new byte[8000];
		      int numRead = porta.readBytes(readBuffer, readBuffer.length);
		      System.out.println("Bytes lidos" + numRead);
		      result = new String(readBuffer);
		      System.out.println("Valor retornado: "+result);
		      return result;
		   }
		} catch (Exception e) { 
			System.out.println("Erro lendo dados do Espectrofotometro");
			e.printStackTrace(); 
		}
		return result;
	}	
}
