# 3.0 DSL 

'metatypes' which include stars, elements and any special constructions
Motivated by the 'schizophrenic object mappings' I needed for monoid actions?

## Drivers 

Require OmegaElement as a "convenience" trait for truth values
Enable more operations on elements, e.g. have =?= built in??
Simplify the action topos construction

Is there a hidden type Linkage[A, AA] which should really have been
encoded as a meta-type in some more advanced DSL?

# Ideas

Might have to have a special wrapping layer applied to each arrow as it's used

More 'self-aware' code 
An object encapsulating the type of the elements in a dot (but shouldn't the dot do that itself?)

if a encaps A, b encaps B then a ^ b encaps A ^ B, etc

move less clunkily between a t: T and a dot: DOT[T]
by having an implicit DOT[T] for each T, so we can say:
x[T : DOT] and not have to pass the dots around, only the types
Then could just pass functions around, not arrows, and have:
f === g :== 
	def ===[S : DOT, T: DOT]: Boolean =
		implicitly[DOT[S]](
			implicitly[[DOT[T]]
		)(f) == xxxx(g)  

But do the dots need to be separate from the types?

