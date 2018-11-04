compile:
	javac ProdAndConsWithRandQuanOfModifiedCells.java

createResults:
	java ProdAndConsWithRandQuanOfModifiedCells 1000 10			> 1000M_10K.dat
	java ProdAndConsWithRandQuanOfModifiedCells 1000 100		> 1000M_100K.dat
	java ProdAndConsWithRandQuanOfModifiedCells 1000 1000		> 1000M_1000K.dat
	java ProdAndConsWithRandQuanOfModifiedCells 10000 10		> 10000M_10K.dat
	java ProdAndConsWithRandQuanOfModifiedCells 10000 100		> 10000M_100K.dat
	java ProdAndConsWithRandQuanOfModifiedCells 10000 1000	> 10000M_1000K.dat
	java ProdAndConsWithRandQuanOfModifiedCells 100000 10		> 100000M_10K.dat
	java ProdAndConsWithRandQuanOfModifiedCells 100000 100	> 100000M_100K.dat
	java ProdAndConsWithRandQuanOfModifiedCells 100000 1000	> 100000M_1000K.dat

graphs:
	graph -T ps < 1000M_10K.dat			> 1000M_10K.ps
	graph -T ps < 1000M_100K.dat		> 1000M_100K.ps
	graph -T ps < 1000M_1000K.dat		> 1000M_1000K.ps
	graph -T ps < 10000M_10K.dat		> 10000M_10K.ps
	graph -T ps < 10000M_100K.dat		> 10000M_100K.ps
	graph -T ps < 10000M_1000K.dat	> 10000M_1000K.ps
	graph -T ps < 100000M_10K.dat		> 100000M_10K.ps
	graph -T ps < 100000M_100K.dat	> 100000M_100K.ps
	graph -T ps < 100000M_1000K.dat	> 100000M_1000K.ps

remove:
	rm *.dat
	rm *.class
