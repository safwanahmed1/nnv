package de.exitgames.api.loadbalancing;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

@SuppressWarnings({"rawtypes", "unchecked"})
public class Extensions {
    /**
     * Merges all keys from addHash into the target. Adds new keys and updates the values of existing keys in target.
     * @param target    The HashMap to update.</param>
     * @param addHash   The HashMap containing data to merge into target.</param>
     */
	public static void merge(HashMap target, HashMap addHash)
    {
        if (addHash == null || target.equals(addHash))
        {
            return;
        }

        for (Object key : addHash.keySet())
        {
            target.put(key, addHash.get(key));
        }
    }

    /// <summary>
    /// Merges keys of type string to target Hashtable.
    /// </summary>
    /// <remarks>
    /// Does not remove keys from target (so non-string keys CAN be in target if they were before).
    /// </remarks>
    /// <param name="target">The target IDicitionary passed in plus all string-typed keys from the addHash.</param>
    /// <param name="addHash">A HashMap that should be merged partly into target to update it.</param>
    public static void mergeStringKeys(HashMap target, HashMap addHash)
    {
        if (addHash == null || target.equals(addHash))
        {
            return;
        }

        for (Object key : addHash.keySet())
        {
        	if (key instanceof String)
        	{
        		target.put(key, addHash.get(key));
        	}
        }
    }

    /// <summary>
    /// This method copies all string-typed keys of the original into a new Hashtable.
    /// </summary>
    /// <remarks>
    /// Does not recurse (!) into hashes that might be values in the root-hash. 
    /// This does not modify the original.
    /// </remarks>
    /// <param name="original">The original IDictonary to get string-typed keys from.</param>
    /// <returns>New Hashtable containing parts ot fht original.</returns>
    public static HashMap stripToStringKeys(HashMap original)
    {
        HashMap target = new HashMap();
        Set<Entry<Object, Object>>  entries = original.entrySet();
        for (Entry<Object,Object> pair : entries)
        {
            if (pair.getKey() instanceof String)
            {
                target.put(pair.getKey(), pair.getValue());
            }
        }

        return target;
    }

    /// <summary>
    /// This removes all key-value pairs that have a null-reference as value.
    /// In Photon properties are removed by setting their value to null.
    /// Changes the original passed HashMap!
    /// </summary>
    /// <param name="original">The HashMap to strip of keys with null-values.</param>
    public static void stripKeysWithNullValues(HashMap original)
    {
        Set<Object> keys = original.keySet();
        for (Object key : keys)
        {
            if (original.get(key) == null)
            {
                original.remove(key);
            }
        }
    }

    /// <summary>
    /// Checks if a particular integer value is in an int-array.
    /// </summary>
    /// <remarks>This might be useful to look up if a particular actorNumber is in the list of players of a room.</remarks>
    /// <param name="target">The array of ints to check.</param>
    /// <param name="nr">The number to lookup in target.</param>
    /// <returns>True if nr was found in target.</returns>
    public static boolean contains(int[] target, int nr)
    {
        if (target == null)
        {
            return false;
        }

        for (int entry : target)
        {
            if (entry == nr)
            {
                return true;
            }
        }

        return false;
    }
}
