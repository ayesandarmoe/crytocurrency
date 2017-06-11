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
		
		// Check (2) and (3)
		for (Transaction.Input i : tx.getInputs()){
			Transaction.Output o = tx.getOutputs().get(i.outputIndex); 
			if (!Crypto.verifySignature(o.address, tx.getRawDataToSign(i.outputIndex), i.signature)){
				//validTx = false;
				return false;
			}else{ // (3)
				totalInputValue += o.value;
				if (claimed.contains(i.prevTxHash)){ //if this utxo is already claimed
					return false;
				}else{// (1)
					for (UTXO utxo : current.getAllUTXO()){
						if (i.prevTxHash != utxo.getTxHash() ){
							return false;
						}else{ // (4)
							if (o.value < 0){
								return false;
							}else{
								totalOutputValue += o.value;
							}
						}
					}
				}
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
					Transaction.Output o = tx.getOutputs().get(i.outputIndex); 

					for (UTXO utxo :current.getAllUTXO()){
						if (current.getTxOutput(utxo) == o ){
							current.removeUTXO(utxo);
						}
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
