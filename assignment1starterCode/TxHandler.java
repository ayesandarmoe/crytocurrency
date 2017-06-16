import java.util.ArrayList;

public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
	UTXOPool current;

    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
		current = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS
		double totalOutputValue = 0.0;
		double totalInputValue = 0.0;
		ArrayList<UTXO> claimed = new ArrayList<UTXO>();
	

		for (Transaction.Input i : tx.getInputs()){
			UTXO prevUtxo = new UTXO(i.prevTxHash, i.outputIndex);
			if ( !current.contains(prevUtxo) ){ // Check 1
				return false;	
			}
			// Check 2
			Transaction.Output o = current.getTxOutput(prevUtxo);
			if ( o.address == null || i.outputIndex < 0 ){
				return false;
			}
			byte[] message = tx.getRawDataToSign(tx.getInputs().indexOf(i));
			if( message == null ){
				return false;
			}
			if (!Crypto.verifySignature(o.address, message, i.signature)){
				return false;
			}else{
				totalInputValue += o.value;
			}
			//Check 3
			if ( claimed.contains(prevUtxo) ){
				return false;
			}else{
				claimed.add(prevUtxo);
			}
			
		}

		for (Transaction.Output o : tx.getOutputs()){
			if ( o.value < 0 ){
				return false;
			}else{
				totalOutputValue += o.value;
			}
		}


		// Check (5)
		return ( totalInputValue >= totalOutputValue);
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
		ArrayList<Transaction> validTxs = new ArrayList<Transaction>();
		for (Transaction tx : possibleTxs){
			if (isValidTx(tx)){
				// get a list of Inputs and remove it from the UTOPool
				for (Transaction.Input i  : tx.getInputs()){
					UTXO utxo = new UTXO(i.prevTxHash, i.outputIndex);

					if ( current.contains(utxo)){
						current.removeUTXO(utxo);
					}
				}
				validTxs.add(tx);
			}
		}
		Transaction[] toReturn = new Transaction[validTxs.size()];
		for (int i =0; i < validTxs.size(); i++){
			toReturn[i] = validTxs.get(i);
		}
		return toReturn;
    }

}
