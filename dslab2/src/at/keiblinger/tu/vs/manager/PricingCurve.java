package at.keiblinger.tu.vs.manager;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Locale;
import java.util.SortedMap;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Thomas Chrenko - 0728121
 *
 */
public class PricingCurve {

	private SortedMap<Integer, Float> pricingCurve;
	private Lock pricingCurveLock;
	
	public PricingCurve() {
		pricingCurve = new TreeMap<Integer, Float>();
		pricingCurve.put(0, 0f);
		pricingCurveLock = new ReentrantLock();
	}
	
	public String setPriceStep(Integer taskCount, Float percent) throws RemoteException {
		pricingCurveLock.lock();
		
		if(taskCount < 0)
			throw new RemoteException("Error: Invalid task count!");
		
		if(percent < 0 || percent > 100)
			throw new RemoteException("Error: Invalid percentage!");
		
		String retValue;
		
		if(pricingCurve.containsKey(taskCount))
			retValue = "Successfully updated price step";
		else
			retValue = "Successfully inserted price step.";	
			
		pricingCurve.put(taskCount, percent);
		
		pricingCurveLock.unlock();
		
		return retValue;
	}
	
	public String getPricingCurve() {
		StringBuilder retValue = new StringBuilder("Task Count | Discount");
		
		pricingCurveLock.lock();
		
		Iterator<Entry<Integer, Float>> iter = pricingCurve.entrySet().iterator();
		
		while(iter.hasNext()) {
			Entry<Integer, Float> entry = iter.next();
			retValue.append("\n");
			retValue.append(entry.getKey());
			retValue.append(" | ");
			retValue.append(String.format(Locale.ENGLISH, "%.2f %%", entry.getValue()));
		}
		
		pricingCurveLock.unlock();
		
		return retValue.toString();
	}
	
	public int getCosts(int taskCount, int duration) {
		float discount = 0;
		for(Entry<Integer, Float> entry : pricingCurve.entrySet()) {
			if(entry.getKey() <= taskCount) {
				discount = entry.getValue();
			}
		}
		
		int costs = duration * 10;
		
		return (int)Math.round((costs * (100 - discount))/100);
	}
}
