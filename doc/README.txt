Per la lanciare il programma: java -jar csvJoiner.jar <inputFile1> <inputFile2> <outputFile>");
Esempio (nota le virgolette per file che hanno spazi nel nome): java -jar csvJoiner.jar fileOriginaleComponenti4E5Prime90MatriciCSV.csv \"ESAW-1-2-d (originale con bottom depth).csv\" fullTestOutput.csv");

Il risultato e' il file che metti come terzo parametro. 
ATTENZIONE - fai attenzione al nome del file di output (terzo parametro): se c'e' gia un file uguale, il file gia' presente viene cancellato e sovrascritto senza che il programma ti dica niente (questo punto lo posso migliorare :) )